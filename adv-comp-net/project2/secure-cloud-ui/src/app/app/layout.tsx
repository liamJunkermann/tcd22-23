"use client";
import Providers from "./providers";

export default function appLayout({ children }: { children: React.ReactNode }) {
  return <Providers>{children}</Providers>;
}
