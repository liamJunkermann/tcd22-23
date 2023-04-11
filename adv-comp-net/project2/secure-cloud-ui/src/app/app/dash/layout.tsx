"use client";
import { NavbarSimple } from "@/components/Navbar/Navbar";
import { useAuthContext } from "@/contexts/auth/auth.context";
import {
  AppShell,
  Burger,
  Header,
  MediaQuery,
  Text,
  useMantineTheme,
} from "@mantine/core";
import { usePathname, useRouter } from "next/navigation";
import { useEffect, useState } from "react";

export default function DashLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const theme = useMantineTheme();
  const [opened, setOpened] = useState(false);
  const pathname = usePathname();
  const { user, userData } = useAuthContext();

  const router = useRouter();

  useEffect(() => {
    function pushLogin() {
      router.push("/app/login");
    }
    if (user == null) pushLogin();
  }, [user, router]);

  return (
    <AppShell
      styles={{
        main: {
          background:
            theme.colorScheme === "dark"
              ? theme.colors.dark[8]
              : theme.colors.gray[0],
          overflowY: "scroll",
        },
      }}
      navbarOffsetBreakpoint="sm"
      asideOffsetBreakpoint="sm"
      navbar={<NavbarSimple active={pathname} hidden={!opened} />}
      header={
        <Header height={{ base: 50, md: 70 }} p="md">
          <div
            style={{ display: "flex", alignItems: "center", height: "100%" }}
          >
            <MediaQuery largerThan="sm" styles={{ display: "none" }}>
              <Burger
                opened={opened}
                onClick={() => setOpened((o) => !o)}
                size="sm"
                color={theme.colors.gray[6]}
                mr="xl"
              />
            </MediaQuery>

            <Text>
              Secure Cloud Application - Welcome{" "}
              {(
                <span style={{ fontWeight: "bold" }}>
                  {userData?.displayName}
                </span>
              ) || (
                <span style={{ fontFamily: "monospace" }}>
                  {user?.email?.split("@")[0]}
                </span>
              )}
            </Text>
          </div>
        </Header>
      }
    >
      {children}
    </AppShell>
  );
}
