import { redirect } from "next/navigation";

export default function LandingPage() {
  redirect("/app/dash");
  return <>Loading</>;
}
