import { useAuthContext } from "@/contexts/auth/auth.context";
import { useKey } from "@/contexts/key/key.context";
import addData from "@/firebase/firestore/addData";
import upload from "@/firebase/storage/upload";
import {
  encryptData,
  encryptUserData,
  generateRandomKey,
} from "@/utils/crypto";
import { Button, FileInput, Overlay, Paper, rem, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { IconUpload } from "@tabler/icons-react";
import { useEffect, useState } from "react";

interface FileEncryptionFormProps {
  onComplete: () => void;
}
export function FileEncryptionForm(props: FileEncryptionFormProps) {
  const [file, setFile] = useState<File | null>(null);
  const [fileText, setFileText] = useState("");
  const [encText, setEncText] = useState("");
  const [encKey, setEncKey] = useState("");

  const [loading, { open: load, close: loaded }] = useDisclosure(false);

  const { user } = useAuthContext();
  const {
    keyState: { userKey },
  } = useKey();

  useEffect(() => {
    if (file != null) {
      const func = async () => {
        setFileText(await file.text());
      };
      func();
    }
  }, [file]);

  function onEncText(text: string) {
    // Generate file specific key
    const randKey = generateRandomKey(32);
    setEncKey(randKey);

    const buf = Buffer.from(randKey, "base64");
    console.log(
      `generated key ${buf.subarray(16, 32).length} and iv ${
        buf.subarray(0, 16).length
      }`
    );
    // encrypt data using random key
    const encData = encryptData(text, randKey);
    setEncText(encData);
    return { key: randKey, data: encData };
  }

  function handleFileChange(payload: File | null) {
    if (payload == null) {
      return;
    }
    setFileText("");
    setEncText("");
    setEncKey("");
    setFile(payload);
    return;
  }

  async function handleEncUpload() {
    if (
      user != null &&
      userKey.publicKey != undefined &&
      file != null &&
      encKey != "" &&
      encText != ""
    ) {
      load();
      // First Upload Encrypted Data
      const up = await upload(`${user.uid}/${file.name}`, encText);

      // next encrypt file key (with current users public key)
      const secured_key = await encryptUserData(
        Buffer.from(encKey, "base64"),
        userKey.publicKey
      );

      // finally upload file ref and secured key
      const { result, error } = await addData(user.uid, file.name, {
        fileRef: up.ref.fullPath,
        secured_key,
      });
      if (error) {
        console.error(`an error occurred during addData, ${error}`);
      }

      console.log(result);
      loaded();
      props.onComplete();
    }
  }

  return (
    <Paper
      radius="md"
      p="lg"
      withBorder
      sx={{ display: "flex", flexDirection: "column", gap: "10px" }}
    >
      <div style={{ display: "flex", flexDirection: "row", gap: 15 }}>
        <div
          style={{
            display: "flex",
            flexDirection: "row",
            flexGrow: 1,
            justifyContent: "flex-start",
            gap: 15,
          }}
        >
          <FileInput
            multiple={false}
            placeholder="File to Encrypt"
            label="Upload your File"
            icon={<IconUpload size={rem(14)} />}
            sx={{ maxWidth: 150 }}
            value={file}
            onChange={handleFileChange}
            disabled={loading}
          />
          <Button
            sx={{ alignSelf: "flex-end" }}
            disabled={fileText == "" || loading}
            onClick={() => onEncText(fileText)}
          >
            Encrypt
          </Button>
          <Button sx={{ alignSelf: "flex-end" }} disabled>
            Decrypt
          </Button>
        </div>
        <div
          style={{
            display: "flex",
            flexGrow: 1,
            flexDirection: "row-reverse",
            alignItems: "flex-end",
            gap: 15,
          }}
        >
          <Button
            sx={{ justifySelf: "flex-end" }}
            color="green"
            disabled={
              !(file != null && encKey != "" && encText != "") || loading
            }
            onClick={handleEncUpload}
            loading={loading}
          >
            Upload
          </Button>
        </div>
      </div>
      {file && (
        <div>
          <pre>
            {file.name} - {file.type}
          </pre>
          {fileText != "" && (
            <div
              style={{
                padding: 10,
                border: "1px solid white",
                backgroundColor: "#1d1d1b",
                fontFamily: "monospace,monospace",
                borderRadius: 5,
              }}
            >
              {file.type == "plain/text"
                ? fileText
                : `preview of type ${file.type} not currently supported`}
            </div>
          )}
        </div>
      )}
      {encKey != "" && (
        <>
          Key
          <div>
            <KeySpoiler keyVal={encKey} />
          </div>
          {/* <pre>{encKey}</pre> */}
        </>
      )}
      {encText != "" && (
        <>
          Text
          <div
            style={{
              padding: 10,
              border: "1px solid white",
              backgroundColor: "#1d1d1b",
              fontFamily: "monospace,monospace",
              wordWrap: "break-word",
              borderRadius: 5,
            }}
          >
            {encText}
          </div>
        </>
      )}
    </Paper>
  );
}

function KeySpoiler({ keyVal }: { keyVal: string }) {
  const [visible, setVisible] = useState(false);
  return (
    <div
      style={{
        // padding: 10,
        border: "1px solid white",
        backgroundColor: "#1d1d1b",
        fontFamily: "monospace,monospace",
        wordWrap: "break-word",
        borderRadius: 5,
        // height: "2rem",
      }}
    >
      <div style={{ position: "relative" }}>
        <Text onClick={() => setVisible(false)} sx={{ padding: 20 }}>
          {keyVal}
        </Text>
        {!visible && (
          <>
            <Overlay blur={15} center sx={{ borderRadius: 5 }}>
              <Button color="red" radius="xl" onClick={() => setVisible(true)}>
                Secret Key, click to reveal
              </Button>
            </Overlay>
          </>
        )}
      </div>
    </div>
  );
}
