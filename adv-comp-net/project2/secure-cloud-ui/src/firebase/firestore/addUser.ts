import { UserDetails } from "@/contexts/auth/auth.context";
import firebase_app from "@/firebase";
import { User } from "firebase/auth";
import { doc, getFirestore, setDoc } from "firebase/firestore";

const db = getFirestore(firebase_app)
export default async function addUser(user: User, details: UserDetails) {
    let result = null;
    let error = null;

    try {
        result = await setDoc(doc(db, "app/users/profiles/"+user.uid), details, {merge: true})
    } catch (e) {
        error = e;
    }

    return {result, error}
}