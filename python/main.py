import cv2
import numpy as np
from ultralytics import YOLO
import os
from deepface import DeepFace
import time
import pickle

# 屏蔽警告
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'


class ProFaceSystem:
    def __init__(self, user_dir='users', yolo_model='yolov8n-face.pt', cache_path='face_db.pkl'):
        self.user_dir = user_dir
        self.cache_path = cache_path
        self.face_db = []
        self.current_results = []
        self.frame_count = 0

        print("\n[1/3] 正在加载 YOLO 侦察兵 (CPU 模式)...")
        self.yolo = YOLO(yolo_model)  # 默认 CPU

        self._load_or_build()
        print(f"[3/3] 系统就绪！")

    def _load_or_build(self):
        if os.path.exists(self.cache_path):
            print(f">>> 发现底库缓存，闪电加载中...")
            with open(self.cache_path, 'rb') as f:
                self.face_db = pickle.load(f)
        else:
            print(f">>> 正在使用 RetinaFace 构建高精度底库 (仅需一次)...")
            self._build_database()

    def _build_database(self):
        """恢复你最喜欢的 RetinaFace + L2 归一化逻辑"""
        if not os.path.exists(self.user_dir):
            os.makedirs(self.user_dir)
            return

        for fname in os.listdir(self.user_dir):
            if fname.lower().endswith(('.jpg', '.jpeg', '.png')):
                path = os.path.join(self.user_dir, fname)
                name = os.path.splitext(fname)[0]
                try:
                    # RetinaFace 虽然慢，但在底库注册时是必须的
                    objs = DeepFace.represent(
                        img_path=path,
                        model_name="ArcFace",
                        detector_backend="retinaface",
                        enforce_detection=True,
                        align=True
                    )
                    embedding = np.array(objs[0]["embedding"])
                    embedding = embedding / np.linalg.norm(embedding)  # 必须 L2 归一化
                    self.face_db.append({"name": name, "embedding": embedding})
                    print(f"  -> [高精度注册成功]: {name}")
                except Exception as e:
                    print(f"  -> 注册失败 {fname}: {e}")

        with open(self.cache_path, 'wb') as f:
            pickle.dump(self.face_db, f)

    def find_match_accurate(self, face_img_bgr):
        """恢复带对齐的识别逻辑"""
        if not self.face_db: return "No Users", (0, 0, 255)
        try:
            # 关键：即便在 CPU 上，也要 align=True，这才是准的核心
            target_objs = DeepFace.represent(
                img_path=face_img_bgr,
                model_name="ArcFace",
                detector_backend="skip",  # 因为 YOLO 已经找好位置了
                align=True
            )
            target_emb = np.array(target_objs[0]["embedding"])
            target_emb = target_emb / np.linalg.norm(target_emb)

            best_name = "Unknown"
            max_sim = -1.0
            for person in self.face_db:
                sim = np.dot(target_emb, person["embedding"])
                if sim > max_sim:
                    max_sim = sim
                    best_name = person["name"]

            # 判定标准
            if max_sim > 0.40:  # CPU 模式建议稍微宽容一点
                color = (0, 255, 0) if max_sim > 0.55 else (0, 255, 255)
                return f"{best_name} ({max_sim * 100:.1f}%)", color
            return f"Unknown ({max_sim * 100:.1f}%)", (0, 0, 255)
        except:
            return "Scan Error", (0, 0, 255)

    def run(self):
        cap = cv2.VideoCapture(0)
        cap.set(cv2.CAP_PROP_FRAME_WIDTH, 640)
        cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 480)

        last_time = time.time()
        while True:
            ret, frame = cap.read()
            if not ret: break
            self.frame_count += 1

            results = self.yolo(frame, conf=0.5, verbose=False)

            # 性能折中：每 4 帧做一次精准识别
            if self.frame_count % 4 == 0:
                temp_results = []
                for result in results:
                    for box in result.boxes:
                        x1, y1, x2, y2 = box.xyxy[0].cpu().numpy().astype(int)
                        # 给 YOLO 的框稍微加点边界，方便 DeepFace 做人脸对齐
                        h, w = frame.shape[:2]
                        pad = 20
                        face_roi = frame[max(0, y1 - pad):min(h, y2 + pad), max(0, x1 - pad):min(w, x2 + pad)]

                        if face_roi.size == 0: continue
                        info, color = self.find_match_accurate(face_roi)
                        temp_results.append({'box': (x1, y1, x2, y2), 'info': info, 'color': color})
                self.current_results = temp_results

            for r in self.current_results:
                x1, y1, x2, y2 = r['box']
                cv2.rectangle(frame, (x1, y1), (x2, y2), r['color'], 2)
                cv2.putText(frame, r['info'], (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.6, r['color'], 2)

            curr_time = time.time()
            fps = 1 / (curr_time - last_time)
            last_time = curr_time
            cv2.putText(frame, f"CPU High-Accuracy: {int(fps)} FPS", (20, 30),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 255), 2)
            cv2.imshow("Library Face System (Stable)", frame)
            if cv2.waitKey(1) & 0xFF == ord('q'): break

        cap.release()
        cv2.destroyAllWindows()


if __name__ == "__main__":
    system = ProFaceSystem()
    system.run()