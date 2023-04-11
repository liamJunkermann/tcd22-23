"use client";

import { ProfileEditor } from "@/components/ProfileEditor/ProfileEditor";
import { useAuthContext } from "@/contexts/auth/auth.context";
import { Container } from "@mantine/core";

export default function ProfilePage() {
  const { user } = useAuthContext();

  if (user) {
    return (
      <Container>
        <ProfileEditor user={user} />
      </Container>
    );
  }
}
