"use client";

import { Container, Title } from "@mantine/core";
import { AuthenticationForm } from "@/components/AuthenticationForm/AuthenticationForm";

export default function LoginPage() {
  return (
    <div style={{ height: "100%" }}>
      <Container size={420} my={40}>
        <Title
          align="center"
          sx={(theme) => ({
            fontFamily: `Greycliff CF, ${theme.fontFamily}`,
            fontWeight: 900,
            padding: 10,
          })}
        >
          Welcome back!
        </Title>
        <AuthenticationForm />
      </Container>
    </div>
  );
}
