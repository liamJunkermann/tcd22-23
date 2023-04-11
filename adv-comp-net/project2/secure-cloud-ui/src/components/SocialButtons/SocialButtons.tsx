import { Button, ButtonProps } from "@mantine/core";
import { MouseEventHandler } from "react";
import { GoogleIcon } from "./GoogleButton";

export function GoogleButton(
  props: ButtonProps & { onClick: MouseEventHandler<HTMLButtonElement> }
  // onClick?: MouseEventHandler<HTMLButtonElement>
) {
  return (
    <Button
      leftIcon={<GoogleIcon />}
      variant="default"
      color="gray"
      {...props}
    />
  );
}
