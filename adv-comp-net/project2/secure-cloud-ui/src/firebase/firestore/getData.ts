import firebase_app from "@/firebase";
import { getFirestore, doc, getDoc } from "firebase/firestore";

const db = getFirestore(firebase_app)
export default async function getData(collection: string, id: string) {

    let result = null;
    let error = null;

    try {
        result = await (await getDoc(doc(db, "app/userData/"+ collection, id))).data();
    } catch (e) {
        error = e;
    }

    return { result, error };
}