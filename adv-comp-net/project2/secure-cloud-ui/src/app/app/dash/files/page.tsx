"use client";

import { FileEncryptionForm } from "@/components/FileEncryptionForm/FileEncryptionForm";
import FileViewer from "@/components/FileViewer/FileViewer";
import { Button, Container, Group, Modal, Paper, Title } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useState } from "react";

export default function EncryptFilePage() {
  const [opened, { open, close }] = useDisclosure(false);
  const [files, setFiles] = useState(0);
  return (
    <>
      <Modal
        opened={opened}
        onClose={close}
        title="Add File"
        centered
        size="auto"
      >
        <Container>
          <FileEncryptionForm
            onComplete={() => {
              close();
              setFiles(files + 1);
            }}
          />
        </Container>
      </Modal>
      <Container>
        <Paper
          radius="md"
          p="xl"
          withBorder
          sx={{ display: "flex", flexDirection: "column", gap: 10 }}
        >
          <Group sx={{ display: "flex", justifyContent: "space-between" }}>
            <Title order={1}>Your Files</Title>
            <Button onClick={open}>Upload File</Button>
          </Group>

          <FileViewer key={`fileviewer-${files}`} />
        </Paper>
      </Container>
    </>
  );
}
