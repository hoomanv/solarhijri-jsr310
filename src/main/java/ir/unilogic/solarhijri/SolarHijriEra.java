/*
 * Copyright 2022 UNILOGIC.IR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.unilogic.solarhijri;

import java.time.DateTimeException;
import java.time.chrono.Era;

/**
 * An era in the Solar Hijri calendar system.
 */
public enum SolarHijriEra implements Era
{
    /**
     * The singleton instance for the era before the current one, 'Before the Hijra', which has the numeric value 0.
     */
    BH,
    /**
     * The singleton instance for the current era, 'Anno Hegirae', which has the numeric value 1.
     */
    AH;

    public static SolarHijriEra of(int era)
    {
        switch (era) {
            case 0:
                return BH;
            case 1:
                return AH;
            default:
                throw new DateTimeException("Invalid era: " + era);
        }
    }

    @Override
    public int getValue()
    {
        return ordinal();
    }
}
