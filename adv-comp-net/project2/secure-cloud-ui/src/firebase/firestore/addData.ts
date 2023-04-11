import firebase_app from "@/firebase";
import { getFirestore, doc, setDoc } from "firebase/firestore";

const db = getFirestore(firebase_app)
export default async function addData(collection: string, id: string, data: Partial<unknown>) {
    let result = null;
    let error = null;

    try {
        result = await setDoc(doc(db, "app/userData/"+ collection, id), data, {
            merge: true,
        });
    } catch (e) {
        error = e;
    }

    return { result, error };
}