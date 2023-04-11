export enum KeyReducerTypes {
  SetKey = "SET_KEY",
  SetUid = "SET_UID",
}

type KeyPayload = {
  [KeyReducerTypes.SetKey]: {
    publicKey: JsonWebKey | undefined;
    privateKey: JsonWebKey | undefined;
  };
  [KeyReducerTypes.SetUid]: string;
};

export type KeyActions = ActionMap<KeyPayload>[keyof ActionMap<KeyPayload>];

export const keyReducer = (
  state: KeyPayload[KeyReducerTypes.SetKey],
  action: KeyActions
) => {
  switch (action.type) {
    case KeyReducerTypes.SetKey:
      return action.payload;
    default:
      return state;
  }
};

export const uidReducer = (
  state: KeyPayload[KeyReducerTypes.SetUid],
  action: KeyActions
) => {
  switch (action.type) {
    case KeyReducerTypes.SetUid:
      return action.payload;
    default:
      return state;
  }
};
