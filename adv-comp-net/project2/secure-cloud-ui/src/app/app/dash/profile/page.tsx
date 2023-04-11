"use client";

import { Container, Paper, Title } from "@mantine/core";

export default function ProfilePage() {
  return (
    <Container>
      <Paper
        radius="md"
        p="xl"
        withBorder
        sx={{ display: "flex", flexDirection: "column", gap: 10 }}
      >
        <Title>Your Profile</Title>
      </Paper>
    </Container>
  );
}
