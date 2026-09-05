import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { GraphFilter, SQLNodeExchangeStageData, SQLNodeStageData } from "../interfaces/AppStore";

export type DurationMode = "exclusive" | "inclusive";

export const initialState: {
  sqlMode: GraphFilter;
  selectedStage: SQLNodeStageData | SQLNodeExchangeStageData | undefined;
  showStages: boolean;
  durationMode: DurationMode;
} = {
  sqlMode: "advanced",
  selectedStage: undefined,
  showStages: true,
  durationMode: "exclusive",
};

const generalSlice = createSlice({
  name: "general",
  initialState,
  reducers: {
    setSQLMode: (
      state,
      action: PayloadAction<{
        newMode: GraphFilter;
      }>,
    ) => {
      state.sqlMode = action.payload.newMode;
    },
    setSelectedStage: (
      state,
      action: PayloadAction<{
        selectedStage: SQLNodeStageData | SQLNodeExchangeStageData | undefined;
      }>,
    ) => {
      state.selectedStage = action.payload.selectedStage;
    },
    setShowStages: (
      state,
      action: PayloadAction<{
        showStages: boolean;
      }>,
    ) => {
      state.showStages = action.payload.showStages;
    },
    setDurationMode: (
      state,
      action: PayloadAction<{
        durationMode: DurationMode;
      }>,
    ) => {
      state.durationMode = action.payload.durationMode;
    },
  },
});

// Export the action creators and the reducer
export const { setSQLMode, setSelectedStage, setShowStages, setDurationMode } = generalSlice.actions;

export default generalSlice.reducer;
