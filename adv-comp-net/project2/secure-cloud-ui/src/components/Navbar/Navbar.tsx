import logout from "@/firebase/auth/logout";
import {
  Code,
  createStyles,
  getStylesRef,
  Group,
  Navbar,
  rem,
} from "@mantine/core";
import { IconFile, IconKey, IconLogout, IconUser } from "@tabler/icons-react";
import Link from "next/link";

const useStyles = createStyles((theme) => ({
  header: {
    paddingBottom: theme.spacing.md,
    marginBottom: `calc(${theme.spacing.md} * 1.5)`,
    borderBottom: `${rem(1)} solid ${
      theme.colorScheme === "dark" ? theme.colors.dark[4] : theme.colors.gray[2]
    }`,
  },

  footer: {
    paddingTop: theme.spacing.md,
    marginTop: theme.spacing.md,
    borderTop: `${rem(1)} solid ${
      theme.colorScheme === "dark" ? theme.colors.dark[4] : theme.colors.gray[2]
    }`,
  },

  link: {
    ...theme.fn.focusStyles(),
    display: "flex",
    alignItems: "center",
    textDecoration: "none",
    fontSize: theme.fontSizes.sm,
    color:
      theme.colorScheme === "dark"
        ? theme.colors.dark[1]
        : theme.colors.gray[7],
    padding: `${theme.spacing.xs} ${theme.spacing.sm}`,
    borderRadius: theme.radius.sm,
    fontWeight: 500,

    "&:hover": {
      backgroundColor:
        theme.colorScheme === "dark"
          ? theme.colors.dark[6]
          : theme.colors.gray[0],
      color: theme.colorScheme === "dark" ? theme.white : theme.black,

      [`& .${getStylesRef("icon")}`]: {
        color: theme.colorScheme === "dark" ? theme.white : theme.black,
      },
    },
  },

  linkIcon: {
    ref: getStylesRef("icon"),
    color:
      theme.colorScheme === "dark"
        ? theme.colors.dark[2]
        : theme.colors.gray[6],
    marginRight: theme.spacing.sm,
  },

  linkActive: {
    "&, &:hover": {
      backgroundColor: theme.fn.variant({
        variant: "light",
        color: theme.primaryColor,
      }).background,
      color: theme.fn.variant({ variant: "light", color: theme.primaryColor })
        .color,
      [`& .${getStylesRef("icon")}`]: {
        color: theme.fn.variant({ variant: "light", color: theme.primaryColor })
          .color,
      },
    },
  },
}));

const data = [
  { link: "files", label: "Files", icon: IconFile },
  { link: "keys", label: "Keys", icon: IconKey },
  // { link: "", label: "Billing", icon: IconReceipt2 },
  // { link: "", label: "Security", icon: IconFingerprint },
  // { link: "", label: "Databases", icon: IconDatabaseImport },
  // { link: "", label: "Authentication", icon: Icon2fa },
  // { link: "", label: "Other Settings", icon: IconSettings },
];

export function NavbarSimple({
  active,
  hidden,
}: {
  active: string;
  hidden: boolean;
}) {
  const { classes, cx } = useStyles();
  //   const [active, setActive] = useState('Billing');

  const regex = /\/app\/dash\//gm;

  const links = data.map((item) => (
    <a
      className={cx(classes.link, {
        [classes.linkActive]: item.link === active.replace(regex, ""),
      })}
      href={item.link}
      key={item.label}
    >
      <item.icon className={classes.linkIcon} stroke={1.5} />
      <span>{item.label}</span>
    </a>
  ));

  return (
    <Navbar
      width={{ sm: 200, lg: 300 }}
      p="md"
      hiddenBreakpoint="sm"
      hidden={hidden}
    >
      <Navbar.Section grow>
        <Group className={classes.header} position="apart">
          Secure Your Cloud
          <Code sx={{ fontWeight: 700 }}>v0.1.0</Code>
        </Group>
        {links}
      </Navbar.Section>

      <Navbar.Section className={classes.footer}>
        <Link
          href="/app/dash/profile"
          className={cx(classes.link, {
            [classes.linkActive]: "profile" === active.replace(regex, ""),
          })}
        >
          <IconUser className={classes.linkIcon} stroke={1.5} />
          Profile
        </Link>
        <a
          href="#"
          className={classes.link}
          onClick={(event) => {
            event.preventDefault();
            logout();
          }}
        >
          <IconLogout className={classes.linkIcon} stroke={1.5} />
          <span>Logout</span>
        </a>
      </Navbar.Section>
    </Navbar>
  );
}
