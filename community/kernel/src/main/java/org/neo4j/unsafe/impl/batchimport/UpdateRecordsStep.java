/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.impl.batchimport;

import java.util.Collection;

import org.neo4j.kernel.impl.store.RecordStore;
import org.neo4j.kernel.impl.store.record.AbstractBaseRecord;
import org.neo4j.unsafe.impl.batchimport.staging.ExecutorServiceStep;
import org.neo4j.unsafe.impl.batchimport.staging.StageControl;
import org.neo4j.unsafe.impl.batchimport.stats.Key;
import org.neo4j.unsafe.impl.batchimport.stats.Keys;
import org.neo4j.unsafe.impl.batchimport.stats.Stat;
import org.neo4j.unsafe.impl.batchimport.stats.StatsProvider;

/**
 * Updates a batch of records to a store.
 */
public class UpdateRecordsStep<RECORD extends AbstractBaseRecord>
        extends ExecutorServiceStep<RECORD[]>
        implements StatsProvider
{
    private final RecordStore<RECORD> store;
    private final int recordSize;
    private int recordsUpdated;

    public UpdateRecordsStep( StageControl control, int workAheadSize, int movingAverageSize,
            RecordStore<RECORD> store )
    {
        super( control, "v", workAheadSize, movingAverageSize, 1 );
        this.store = store;
        this.recordSize = store.getRecordSize();
    }

    @Override
    protected Object process( long ticket, RECORD[] batch )
    {
        for ( RECORD record : batch )
        {
            if ( record != null )
            {
                store.updateRecord( record );
            }
        }
        recordsUpdated += batch.length;
        return null; // end of line
    }

    @Override
    protected void collectStatsProviders( Collection<StatsProvider> into )
    {
        super.collectStatsProviders( into );
        into.add( this );
    }

    @Override
    public Stat stat( Key key )
    {
        return key == Keys.io_throughput ? new IoThroughputStat( startTime, endTime, recordSize*recordsUpdated ) : null;
    }

    @Override
    public Key[] keys()
    {
        return new Keys[] {Keys.io_throughput};
    }
}
