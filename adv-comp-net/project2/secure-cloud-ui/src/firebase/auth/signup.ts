import firebase_app from "@/firebase";

import { createUserWithEmailAndPassword, getAuth } from "firebase/auth";
import addUser from "../firestore/addUser";

const auth = getAuth(firebase_app);

export default async function signUp(name: string, email: string, password: string) {
  let result = null,
    error = null;
  try {
    result = await createUserWithEmailAndPassword(auth, email, password);
  } catch (e) {
    error = e;
  }

  if (result != null) {
    const {error: addErr} = await addUser(result.user, {displayName: name, email: email})
    if (addErr) {
      return {result: null, error: `addUser error ${addErr}`};
    }
  }

  return { result, error };
}
