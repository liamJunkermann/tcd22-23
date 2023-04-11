import { getContext, storeContext } from "@/utils/context.util";
import React from "react";
import { KeyActions, keyReducer, uidReducer } from "./key.reducer";

type KeyStateType = {
  userKey: {
    publicKey: JsonWebKey | undefined;
    privateKey: JsonWebKey | undefined;
  };
  uid: string;
};
const nullKeyState = {
  userKey: {
    publicKey: undefined,
    privateKey: undefined,
  },
  uid: "",
};

const KEY_STORAGE_TOKEN = "key";

const initialState = () => {
  const keyObj = getContext<KeyStateType>(KEY_STORAGE_TOKEN);
  if (keyObj) {
    const { context } = keyObj;
    if (context.userKey?.privateKey && context.userKey?.publicKey) {
      return context;
    }
  }
  return nullKeyState;
};

export const KeyContext = React.createContext<{
  keyState: KeyStateType;
  keyDispatch: React.Dispatch<KeyActions>;
}>({ keyState: initialState(), keyDispatch: () => null });
KeyContext.displayName = "KeyContext";

const keyContextReducer = (
  { userKey, uid }: KeyStateType,
  action: KeyActions
) => ({
  userKey: keyReducer(userKey, action),
  uid: uidReducer(uid, action),
});

export const useKey = () => React.useContext(KeyContext);
export function KeyProvider({ children }: { children?: React.ReactNode }) {
  const [keyState, keyDispatch] = React.useReducer(
    keyContextReducer,
    initialState()
  );

  React.useEffect(() => {
    storeContext<KeyStateType>(KEY_STORAGE_TOKEN, keyState);
  }, [keyState]);

  return (
    <KeyContext.Provider value={{ keyState, keyDispatch }}>
      {children}
    </KeyContext.Provider>
  );
}
