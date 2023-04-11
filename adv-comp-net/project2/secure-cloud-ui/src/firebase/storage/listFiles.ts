import firebase_app from "@/firebase";
import {
    getStorage, listAll, ref,
    StorageReference
} from "firebase/storage";

const storage = getStorage(firebase_app);
export default async function listFiles(path: string) {
  const storageRef = ref(storage, path);

  return getFileNames(storageRef)
}

async function getFileNames(ref: StorageReference): Promise<string[]> {
  const list = await listAll(ref);
  const itemList = list.items.map((itemRef) => itemRef.name);
  const prefixList = await (
    await Promise.all(
      list.prefixes.flatMap(async (folderRef) => await getFileNames(folderRef))
    )
  ).flat();
  return itemList.concat(prefixList);
}

// function listAll(list: ListResult) {
//     const itemList = list.items.map(itemRef => itemRef.fullPath)
//     const folderList = list.prefixes.map(folderRef => listAll(folderRef))
// }
