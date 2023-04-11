import firebase_app from "@/firebase";
import {
    getStorage,
    ref, uploadString
} from "firebase/storage";

const storage = getStorage(firebase_app);
export default async function upload(path: string, data: string) {
  const storageRef = ref(storage, path);
  return await uploadString(storageRef, data)
}
