package com.ventivader.models

data class SolenoidParameters(var inhaleSec: Int = 1,
                              var inhaleHoldSec: Int = 2,
                              var exhaleSec: Int = 3,
                              var exhaleHoldSec: Int = 4,
                              var numberVentCycles: Int = 5)
