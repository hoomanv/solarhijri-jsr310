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

import java.lang.reflect.Method;
import java.time.chrono.AbstractChronology;
import java.time.chrono.Chronology;
import java.time.chrono.Era;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Solar Hijri calendar system.
 * <p>
 * This chronology defines the rules of the Solar Hijri calendar system. This calendar system is primarily used in Iran
 * and Afghanistan.
 */
public class SolarHijriChronology extends AbstractChronology
{
    /**
     * Singleton instance of the Solar Hijri chronology.
     */
    public static final SolarHijriChronology INSTANCE;

    static {
        INSTANCE = new SolarHijriChronology();
        try {
            Method method = AbstractChronology.class.getDeclaredMethod("registerChrono", Chronology.class);
            method.setAccessible(true);
            method.invoke(null, INSTANCE);
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Logger.getLogger(SolarHijriChronology.class.getName())
                    .log(Level.WARNING, "Could not register SolarHijri chronology.", ex);
        }
    }

    private SolarHijriChronology()
    {
    }

    @Override
    public String getId()
    {
        return "Solar-hijri";
    }

    @Override
    public String getCalendarType()
    {
        return "persian"; // See http://unicode.org/reports/tr35/
    }

    @Override
    public SolarHijriDate date(int prolepticYear, int monthOfYear, int dayOfMonth)
    {
        return SolarHijriDate.of(prolepticYear, monthOfYear, dayOfMonth);
    }

    @Override
    public SolarHijriDate dateYearDay(int prolepticYear, int dayOfYear)
    {
        return SolarHijriDate.ofYearDay(prolepticYear, dayOfYear);
    }

    @Override
    public SolarHijriDate dateEpochDay(long epochDay)
    {
        return SolarHijriDate.ofEpochDay(epochDay);
    }

    @Override
    public SolarHijriDate date(TemporalAccessor temporal)
    {
        return SolarHijriDate.from(temporal);
    }

    @Override
    public boolean isLeapYear(long prolepticYear)
    {
        return Math.floorMod(25 * prolepticYear + 11, 33) < 8;
    }

    @Override
    public int prolepticYear(Era era, int yearOfEra)
    {
        if (era instanceof SolarHijriEra)
            return (era == SolarHijriEra.AH ? yearOfEra : 1 - yearOfEra);
        throw new ClassCastException("Era must be SolarHijriEra");
    }

    @Override
    public Era eraOf(int eraValue)
    {
        return SolarHijriEra.of(eraValue);
    }

    @Override
    public List<Era> eras()
    {
        return Arrays.asList(SolarHijriEra.values());
    }

    @Override
    public ValueRange range(ChronoField field)
    {
        switch (field) {
            case DAY_OF_MONTH:
                return ValueRange.of(1, 29, 31);
            default:
                return field.range();
        }
    }

}
