import { Box } from "@mui/material";
import * as React from "react";
import ConfigTable from "../components/ConfigTable";
import { useAppSelector } from "../Hooks";

export default function ConfigurationTab() {
  const configs = useAppSelector(
    (state) => state.spark.config?.configs,
  )?.filter(
    (row) => row.category === "general" || row.category === "executor-memory",
  );

  return (
    <div
      style={{
        height: "100%",
        display: "flex",
        justifyContent: "center",
        margin: 10,
      }}
    >
      <Box margin="10" width="90%">
        {!!configs && <ConfigTable config={configs} />}
      </Box>
    </div>
  );
}
