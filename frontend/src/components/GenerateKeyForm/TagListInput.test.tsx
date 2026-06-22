import { describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { TagListInput } from "./TagListInput";

describe("TagListInput", () => {
  it("adds a trimmed value when the Add button is clicked", () => {
    const onChange = vi.fn();
    render(<TagListInput label="Scopes" placeholder="e.g. read:orders" values={[]} onChange={onChange} />);

    fireEvent.change(screen.getByPlaceholderText("e.g. read:orders"), { target: { value: "  read:orders  " } });
    fireEvent.click(screen.getByRole("button", { name: /add/i }));

    expect(onChange).toHaveBeenCalledWith(["read:orders"]);
  });

  it("adds a value when Enter is pressed", () => {
    const onChange = vi.fn();
    render(<TagListInput label="Scopes" placeholder="x" values={[]} onChange={onChange} />);

    const input = screen.getByPlaceholderText("x");
    fireEvent.change(input, { target: { value: "write:orders" } });
    fireEvent.keyDown(input, { key: "Enter" });

    expect(onChange).toHaveBeenCalledWith(["write:orders"]);
  });

  it("does not add a blank value", () => {
    const onChange = vi.fn();
    render(<TagListInput label="Scopes" placeholder="x" values={[]} onChange={onChange} />);
    fireEvent.click(screen.getByRole("button", { name: /add/i }));
    expect(onChange).not.toHaveBeenCalled();
  });

  it("does not add a duplicate value", () => {
    const onChange = vi.fn();
    render(<TagListInput label="Scopes" placeholder="x" values={["read:orders"]} onChange={onChange} />);

    fireEvent.change(screen.getByPlaceholderText("x"), { target: { value: "read:orders" } });
    fireEvent.click(screen.getByRole("button", { name: /add/i }));

    expect(onChange).not.toHaveBeenCalled();
  });

  it("removes a value when its chip is clicked", () => {
    const onChange = vi.fn();
    render(<TagListInput label="Scopes" placeholder="x" values={["a", "b"]} onChange={onChange} />);

    fireEvent.click(screen.getByRole("button", { name: "Remove a" }));

    expect(onChange).toHaveBeenCalledWith(["b"]);
  });
});
