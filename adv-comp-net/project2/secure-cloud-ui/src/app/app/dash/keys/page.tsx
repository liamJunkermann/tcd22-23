"use client";
import { useAuthContext } from "@/contexts/auth/auth.context";
import { useKey } from "@/contexts/key/key.context";
import { KeyReducerTypes } from "@/contexts/key/key.reducer";
import { generateUserKeys } from "@/utils/crypto";
import { Button, CopyButton, Paper, Text } from "@mantine/core";
import { IconKey } from "@tabler/icons-react";

export default function KeyPage() {
  return (
    <>
      <Paper radius="md" p="xl" withBorder>
        <KeyManagerPage />
      </Paper>
    </>
  );
}

function KeyManagerPage() {
  const { user } = useAuthContext();
  const {
    keyState: { userKey, uid },
    keyDispatch: dispatch,
  } = useKey();

  async function generateKeys() {
    if (user) {
      console.log("generating key pair");
      const keyPair = await generateUserKeys();
      console.log(`generated pair ${JSON.stringify(keyPair)}`);
      dispatch({ type: KeyReducerTypes.SetKey, payload: keyPair });
      dispatch({ type: KeyReducerTypes.SetUid, payload: user?.uid });
      console.log("dispatched");
    }
  }

  if (
    userKey.privateKey != undefined &&
    userKey.publicKey != undefined &&
    uid === user?.uid
  ) {
    return (
      <>
        <Text>Found Keys!</Text>
        <CopyButton value={JSON.stringify(userKey.publicKey)}>
          {({ copied, copy }) => (
            <Button color={copied ? "teal" : "blue"} onClick={copy}>
              {copied ? "Copied public key" : "Copy public key"}
            </Button>
          )}
        </CopyButton>
      </>
    );
  } else
    return (
      <>
        <Text>Couldn&apos;t find any key pairs</Text>
        <Button leftIcon={<IconKey size="1rem" />} onClick={generateKeys}>
          Generate user key set
        </Button>
      </>
    );
}
