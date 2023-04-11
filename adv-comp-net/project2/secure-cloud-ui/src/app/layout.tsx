import "./globals.css";

export const metadata = {
  title: "Secure Cloud",
  description: "A web experience to secure your cloud!",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
