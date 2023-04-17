import firebase_app from "@/firebase";
import { getBytes, getStorage, ref } from "firebase/storage";

const storage = getStorage(firebase_app)
export default async function download(path: string) {
    const storageRef = ref(storage, path)
    const blob = await getBytes(storageRef)

    return blob;
}