import { useAuthContext } from "@/contexts/auth/auth.context";
import { useKey } from "@/contexts/key/key.context";
import getData from "@/firebase/firestore/getData";
import download from "@/firebase/storage/download";
import listFiles from "@/firebase/storage/listFiles";
import { decryptData, decryptUserData } from "@/utils/crypto";
import { LoadingOverlay } from "@mantine/core";
import { useEffect, useState } from "react";
import { RowData, TableSort } from "../TableSort/TableSort";
import { UserDocumentData } from "@/constants";
import { DocumentData } from "firebase/firestore";

export default function FileViewer() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<RowData[]>();

  const { user } = useAuthContext();
  const {
    keyState: { userKey },
  } = useKey();
  useEffect(() => {
    async function getFiles() {
      if (user) {
        const files = await listFiles(user.uid);
        setData(
          files.map((ref) => ({
            filename: ref.name,
            owner: "you",
            fullPath: ref.fullPath,
          }))
        );
        setLoading(false);
      }
      return;
    }
    getFiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function downloadFile(file: RowData) {
    const bytes = await download(file.fullPath);
    if (user) {
      const { result, error } = await getData(user.uid, file.filename);
      if (error) {
        console.warn(error);
        return;
      }

      if (result && validDoc(result)) {
        console.log(result);
        console.log(bytes);
        console.log("decrypting key");
        const key = await decryptKey(result.secured_key);

        if (key.error) {
          console.error(`key error ${key.error}`);
          return;
        }
        if (key.result) {
          console.log(`decrypted key ${key.result}`);
          console.log("decrypting data");
          try {
            const decData = await decryptData(
              Buffer.from(bytes).toString(),
              key.result
            );
            console.log(decData);
          } catch (e) {
            console.error(e);
          }
        }
      } else {
        console.log("no result");
      }
    }
  }

  const validDoc = (result: DocumentData): result is UserDocumentData => {
    if ((result as UserDocumentData).fileRef) {
      return true;
    } else {
      return false;
    }
  };

  async function decryptKey(secured_key: string) {
    let result = null,
      error = null;
    if (userKey?.privateKey) {
      console.log(`secured_key buffer: ${Buffer.from(secured_key).toString()}`);
      try {
        const key = await decryptUserData(
          Buffer.from(secured_key),
          userKey.privateKey
        );
        result = key;
      } catch (e) {
        console.error(e);
        error = e;
      }
    } else {
      error = "no user key";
    }

    return { result, error };
  }

  return (
    <>
      {/* // <Paper radius="md" p="xl" withBorder> */}
      {loading && <LoadingOverlay visible />}
      {data && (
        <TableSort
          data={data}
          onAction={(row, action) => {
            console.log(`path: ${row.fullPath}, action: ${action}`);
            if (action == "download") {
              downloadFile(row);
            }
          }}
        />
      )}
      {/* // </Paper> */}
    </>
  );
}
