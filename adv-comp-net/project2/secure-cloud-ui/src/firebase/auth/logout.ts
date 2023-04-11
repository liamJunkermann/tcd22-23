import firebase_app from "@/firebase";
import { getAuth } from "firebase/auth";

const auth = getAuth(firebase_app)
export default async function logout() {
    console.log("logging out")
    return await auth.signOut()
}