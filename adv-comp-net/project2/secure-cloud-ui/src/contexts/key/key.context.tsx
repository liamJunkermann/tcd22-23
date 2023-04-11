import { getContext, storeContext } from "@/utils/context.util";
import React from "react";
import { KeyActions, keyReducer } from "./key.reducer";

type KeyStateType = {
  userKey: {
    publicKey: JsonWebKey | undefined;
    privateKey: JsonWebKey | undefined;
  };
};
const nullKeyState = {
  userKey: {
    publicKey: undefined,
    privateKey: undefined,
  },
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

const keyContextReducer = ({ userKey }: KeyStateType, action: KeyActions) => ({
  userKey: keyReducer(userKey, action),
});

export const useKey = () => React.useContext(KeyContext);
export function KeyProvider({ children }: { children?: React.ReactNode }) {
  const [keyState, keyDispatch] = React.useReducer(
    keyContextReducer,
    initialState()
  );

  React.useEffect(() => {
    console.log("keystate change");
    storeContext<KeyStateType>(KEY_STORAGE_TOKEN, keyState);
  }, [keyState]);

  return (
    <KeyContext.Provider value={{ keyState, keyDispatch }}>
      {children}
    </KeyContext.Provider>
  );
}
