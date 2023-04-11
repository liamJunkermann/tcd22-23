import { AuthContextProvider } from "@/contexts/auth/auth.context";
import { KeyProvider } from "@/contexts/key/key.context";
import { MantineProvider } from "@mantine/core";

export default function Providers({ children }: { children: React.ReactNode }) {
  return (
    <MantineProvider
      withGlobalStyles
      withNormalizeCSS
      theme={{
        /** Put your mantine theme override here */
        colorScheme: "dark",
      }}
    >
      <KeyProvider>
        <AuthContextProvider>{children}</AuthContextProvider>
      </KeyProvider>
    </MantineProvider>
  );
}
