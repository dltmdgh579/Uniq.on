import { ElementType, forwardRef, Ref } from "react";
import { AlertProps } from "@/types/props";

/**
 * @params
 * @return
 */
function Alert<T extends ElementType = "div">(
  { as, ...props }: AlertProps<T>,
  ref: Ref<any>
) {
  const target = as ?? "div";
  const Component = target;

  return <Component ref={ref} {...props} />;
}

export default forwardRef(Alert) as typeof Alert;
