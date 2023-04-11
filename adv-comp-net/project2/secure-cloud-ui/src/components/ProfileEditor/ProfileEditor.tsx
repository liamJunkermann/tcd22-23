import {
  Button,
  createStyles,
  Group,
  Paper,
  Text,
  TextInput,
  Title,
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { upperFirst, useToggle } from "@mantine/hooks";
import { IconAt } from "@tabler/icons-react";
import { User } from "firebase/auth";

interface ProfileEditorProps {
  user: User;
}

const useStyles = createStyles((theme) => ({
  icon: {
    color:
      theme.colorScheme === "dark"
        ? theme.colors.dark[3]
        : theme.colors.gray[5],
  },

  name: {
    fontFamily: `Greycliff CF, ${theme.fontFamily}`,
  },
}));

export function ProfileEditor(props: ProfileEditorProps) {
  const { displayName, email } = props.user;
  const { classes } = useStyles();
  const [editable, toggleEditable] = useToggle(["edit", "save"]);

  const form = useForm({
    initialValues: { displayName: displayName || "", email: email || "" },
    validate: {
      email: (val) => (/^\S+@\S+$/.test(val) ? null : "Invalid email"),
    },
  });

  return (
    <Paper
      radius="md"
      p="xl"
      withBorder
      sx={{ display: "flex", flexDirection: "column", gap: 10 }}
    >
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <Title>Your Profile</Title>
        <div>
          <Button
            sx={{ display: editable === "save" ? "none" : "block" }}
            type={editable === "save" ? "submit" : "button"}
            color={editable === "save" ? "green" : "blue"}
            onClick={() => {
              toggleEditable();
            }}
          >
            {upperFirst(editable)}
          </Button>
        </div>
      </div>
      <>
        {editable === "save" ? (
          <form
            onSubmit={form.onSubmit(async (values) => {
              // code to handle profile update from firebase
              console.log(values);
              toggleEditable();
            })}
          >
            <TextInput
              label="Name"
              placeholder="Your Name"
              value={form.values.displayName}
              onChange={(evt) =>
                form.setFieldValue("displayName", evt.currentTarget.value)
              }
              radius="md"
            />
            <TextInput
              required
              label="Email"
              placeholder="your@email.tld"
              value={form.values.email}
              onChange={(event) =>
                form.setFieldValue("email", event.currentTarget.value)
              }
              error={form.errors.email && "Invalid email"}
              radius="md"
            />
            <Group position="right" mt="md">
              <Button type="submit" color="green">
                Save
              </Button>
            </Group>
          </form>
        ) : (
          <>
            <div>
              <Text fz="lg" fw={500} className={classes.name}>
                {displayName || (
                  <span style={{ fontFamily: "monospace" }}>
                    {email?.split("@")[0]}
                  </span>
                )}
              </Text>

              <Group noWrap spacing={10} mt={3}>
                <IconAt stroke={1.5} size="1rem" className={classes.icon} />
                <Text fz="xs" c="dimmed">
                  {email}
                </Text>
              </Group>
            </div>
          </>
        )}
      </>
    </Paper>
  );
}
