import { redirect } from "next/navigation";

export default function Dashboard() {
  redirect("/app/dash/files");
  // return <>Dashboard</>;
}
