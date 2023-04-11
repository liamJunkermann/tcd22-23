import { useAuthContext } from "@/contexts/auth/auth.context";
import listFiles from "@/firebase/storage/listFiles";
import { LoadingOverlay } from "@mantine/core";
import { useEffect, useState } from "react";
import { RowData, TableSort } from "../TableSort/TableSort";

export default function FileViewer() {
  const [loading, setLoading] = useState(true);
  const [data, setData] = useState<RowData[]>();

  const { user } = useAuthContext();
  useEffect(() => {
    async function getFiles() {
      if (user) {
        const files = await listFiles(user.uid);
        setData(files.map((filename) => ({ filename, owner: "you" })));
        setLoading(false);
      }
      return;
    }
    getFiles();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <>
      {/* // <Paper radius="md" p="xl" withBorder> */}
      {loading && <LoadingOverlay visible />}
      {data && <TableSort data={data} />}
      {/* // </Paper> */}
    </>
  );
}
