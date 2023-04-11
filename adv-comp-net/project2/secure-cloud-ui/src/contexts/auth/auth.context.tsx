import firebase_app from "@/firebase";
import getUser from "@/firebase/firestore/getUser";
import { getAuth, onAuthStateChanged, User } from "firebase/auth";
import React from "react";

export interface UserDetails {
  displayName: string;
  email: string;
  publicKey?: string;
}
const auth = getAuth(firebase_app);

export const AuthContext = React.createContext<{
  user: User | undefined;
  userData: UserDetails | undefined;
}>({
  user: undefined,
  userData: undefined,
});

export const useAuthContext = () => React.useContext(AuthContext);

export const AuthContextProvider = ({
  children,
}: {
  children: React.ReactNode;
}) => {
  const [user, setUser] = React.useState<User>();
  const [userData, setUserData] = React.useState<UserDetails>();
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, async (user) => {
      if (user != null) {
        setUser(user);
        const data = await getUser(user);
        if (data.error) {
          console.error(data.error);
        } else {
          if (data.result != null) {
            console.log("got data result, ", data.result);
            setUserData(data.result as UserDetails);
          }
        }
      } else {
        setUser(undefined);
      }
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  return (
    <AuthContext.Provider value={{ user, userData }}>
      {loading ? <div>Loading...</div> : children}
    </AuthContext.Provider>
  );
};
