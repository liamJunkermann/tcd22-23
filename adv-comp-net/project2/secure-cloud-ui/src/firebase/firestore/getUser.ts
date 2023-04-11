import firebase_app from "@/firebase";
import { User } from "firebase/auth";
import { doc, getDoc, getFirestore } from "firebase/firestore";

const db = getFirestore(firebase_app)
export default async function getUser(user: User) {
    let result = null;
    let error = null;

    try {
        const res = await getDoc(doc(db, "app/users/profiles/"+user.uid))
        result = res.data() || null
    } catch (e) {
        error = e;
    }

    return {result, error}
}