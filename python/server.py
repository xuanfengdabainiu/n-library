import cv2
import numpy as np
from ultralytics import YOLO
import os
from deepface import DeepFace
import pickle
import base64
from fastapi import FastAPI
from pydantic import BaseModel
import uvicorn
from typing import Optional

# 屏蔽日志
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'

app = FastAPI()


# --- 请求模型 ---
class FaceRequest(BaseModel):
    image_base64: str
    username: Optional[str] = None  # 注册时需要


class FaceSystem:
    def __init__(self):
        self.user_dir = 'users'
        self.cache_path = 'face_db.pkl'
        if not os.path.exists(self.user_dir): os.makedirs(self.user_dir)

        print(">>> [系统初始化] 加载 YOLO & ArcFace...")
        self.yolo = YOLO('yolov8n-face.pt')
        self.model_name = "ArcFace"

        self.face_db = []
        self._load_db()

    def _load_db(self):
        if os.path.exists(self.cache_path):
            with open(self.cache_path, 'rb') as f:
                self.face_db = pickle.load(f)
            print(f" -> 已加载底库，共 {len(self.face_db)} 人")

    def _save_db(self):
        with open(self.cache_path, 'wb') as f:
            pickle.dump(self.face_db, f)

    def extract_embedding(self, frame):
        """通用特征提取逻辑：YOLO检测 + DeepFace对齐提取"""
        results = self.yolo(frame, conf=0.5, verbose=False)
        for result in results:
            for box in result.boxes:
                x1, y1, x2, y2 = box.xyxy[0].cpu().numpy().astype(int)
                # 扩框 Padding
                h, w = frame.shape[:2]
                pad = 20
                x1, y1, x2, y2 = max(0, x1 - pad), max(0, y1 - pad), min(w, x2 + pad), min(h, y2 + pad)
                face_roi = frame[y1:y2, x1:x2]

                if face_roi.size == 0: continue

                # 对齐并提取
                objs = DeepFace.represent(
                    img_path=face_roi, model_name=self.model_name,
                    detector_backend="skip", align=True, enforce_detection=False
                )
                embedding = np.array(objs[0]["embedding"])
                return embedding / np.linalg.norm(embedding)  # 归一化
        return None

    def register(self, username, base64_str):
        """【新增】注册逻辑：存图 + 提特征 + 存入PKL"""
        try:
            # 1. 解码并保存图片到 users 文件夹
            img_data = base64.b64decode(base64_str.split(",")[1] if "," in base64_str else base64_str)
            nparr = np.frombuffer(img_data, np.uint8)
            frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            img_path = os.path.join(self.user_dir, f"{username}.jpg")
            cv2.imwrite(img_path, frame)

            # 2. 提取特征
            emb = self.extract_embedding(frame)
            if emb is not None:
                # 3. 更新内存和物理缓存
                self.face_db.append({"name": username, "embedding": emb})
                self._save_db()
                print(f" -> [注册成功] 用户: {username}")
                return {"success": True, "face_id": username}  # 返回 username 作为 face_id
            return {"success": False, "message": "未检测到清晰面部"}
        except Exception as e:
            return {"success": False, "message": str(e)}

    def recognize(self, base64_str):
        """识别逻辑：字段对齐 Java"""
        try:
            img_data = base64.b64decode(base64_str.split(",")[1] if "," in base64_str else base64_str)
            nparr = np.frombuffer(img_data, np.uint8)
            frame = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            target_emb = self.extract_embedding(frame)
            if target_emb is None:
                return {"success": False, "message": "未发现人脸"}

            best_match = "Unknown"
            max_sim = 0.0
            for person in self.face_db:
                sim = np.dot(target_emb, person['embedding'])
                if sim > max_sim:
                    max_sim = sim
                    best_match = person['name']

            threshold = 0.40  # 稍微调高一点保证准确性
            if max_sim > threshold:
                return {"success": True, "face_id": best_match, "score": float(max_sim)}
            return {"success": False, "message": "库中无匹配面部"}
        except Exception as e:
            return {"success": False, "message": str(e)}


system = FaceSystem()


@app.post("/api/recognize")
async def recognize_face(req: FaceRequest):
    return system.recognize(req.image_base64)


@app.post("/api/register")
async def register_face(req: FaceRequest):
    return system.register(req.username, req.image_base64)


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)