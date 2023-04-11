export enum KeyReducerTypes {
  SetKey = "SET_KEY",
}

type KeyPayload = {
  [KeyReducerTypes.SetKey]: {
    publicKey: JsonWebKey | undefined;
    privateKey: JsonWebKey | undefined;
  };
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
