package com.activeandroid.test.query;

/*
 * Copyright (C) 2010 Michael Pardo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.test.MockModel;
import android.provider.BaseColumns;

public class FromTest extends SqlableTestCase {
	private static final String SELECT_PREFIX = "SELECT ALL * FROM MockModel ";

	public void testLimit() {
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10",
				from().limit(10));
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10",
				from().limit("10"));
	}

	public void testOffset() {
		assertSqlEquals(SELECT_PREFIX + "OFFSET 10",
				from().offset(10));
		assertSqlEquals(SELECT_PREFIX + "OFFSET 10",
				from().offset("10"));
	}

	public void testLimitOffset() {
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10 OFFSET 20",
				from().offset(20).limit(10));
		assertSqlEquals(SELECT_PREFIX + "LIMIT 10 OFFSET 20",
				from().limit(10).offset(20));
	}

	public void testAs() {
		assertSqlEquals(SELECT_PREFIX + "AS a",
				from().as("a"));
	}

	public void testOrderBy() {
		assertSqlEquals(SELECT_PREFIX + "ORDER BY " + BaseColumns._ID + " DESC",
				from().orderBy( BaseColumns._ID + " DESC"));
	}

	public void testWhereNoArguments() {
		assertSqlEquals(SELECT_PREFIX + "WHERE " + BaseColumns._ID + " = 5",
				from().where( BaseColumns._ID + " = 5"));

		assertSqlEquals(SELECT_PREFIX + "WHERE " + BaseColumns._ID + " = 5",
				from().where( BaseColumns._ID + " = 1").where( BaseColumns._ID + " = 2").where( BaseColumns._ID + " = 5"));
	}

	public void testWhereWithArguments() {
		From query = from().where( BaseColumns._ID + " = ?", 5);
		assertArrayEquals(query.getArguments(), "5");
		assertSqlEquals(SELECT_PREFIX + "WHERE " + BaseColumns._ID + " = ?",
				query);

		query = from().where( BaseColumns._ID + " > ? AND " + BaseColumns._ID + " < ?", 5, 10);
		assertArrayEquals(query.getArguments(), "5", "10");
		assertSqlEquals(SELECT_PREFIX + "WHERE " + BaseColumns._ID + " > ? AND " + BaseColumns._ID + " < ?",
				query);

		query = from()
				.where( BaseColumns._ID + " != ?", 10)
				.where( BaseColumns._ID + " IN (?, ?, ?)", 5, 10, 15)
				.where( BaseColumns._ID + " > ? AND " + BaseColumns._ID + " < ?", 5, 10);
		assertArrayEquals(query.getArguments(), "5", "10");
		assertSqlEquals(SELECT_PREFIX + "WHERE " + BaseColumns._ID + " > ? AND " + BaseColumns._ID + " < ?",
				query);
	}

	public void testSingleJoin() {
		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel ON MockModel." +  BaseColumns._ID + " = JoinModel." +  BaseColumns._ID,
				from().join(JoinModel.class).on("MockModel." +  BaseColumns._ID + " = JoinModel." +  BaseColumns._ID));

		assertSqlEquals(SELECT_PREFIX + "AS a JOIN JoinModel AS b ON a." +  BaseColumns._ID + " = b." +  BaseColumns._ID,
				from().as("a").join(JoinModel.class).as("b").on("a." +  BaseColumns._ID + " = b." +  BaseColumns._ID));

		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel USING (" +  BaseColumns._ID + ", other)",
				from().join(JoinModel.class).using( BaseColumns._ID, "other"));
	}

	public void testJoins() {
		assertSqlEquals(SELECT_PREFIX + "JOIN JoinModel ON " + BaseColumns._ID + " JOIN JoinModel2 ON " +  BaseColumns._ID,
				from().join(JoinModel.class).on( BaseColumns._ID)
				.join(JoinModel2.class).on( BaseColumns._ID));
	}

	public void testJoinTypes() {
		assertSqlEquals(SELECT_PREFIX + "INNER JOIN JoinModel ON",
				from().innerJoin(JoinModel.class).on(""));
		assertSqlEquals(SELECT_PREFIX + "OUTER JOIN JoinModel ON",
				from().outerJoin(JoinModel.class).on(""));
		assertSqlEquals(SELECT_PREFIX + "CROSS JOIN JoinModel ON",
				from().crossJoin(JoinModel.class).on(""));
	}

	public void testGroupByHaving() {
		assertSqlEquals(SELECT_PREFIX + "GROUP BY " +  BaseColumns._ID,
				from().groupBy( BaseColumns._ID));
		assertSqlEquals(SELECT_PREFIX + "GROUP BY " + BaseColumns._ID + " HAVING " + BaseColumns._ID + " = 1",
				from().groupBy( BaseColumns._ID).having( BaseColumns._ID + " = 1"));
		assertSqlEquals(SELECT_PREFIX + "GROUP BY " + BaseColumns._ID + " HAVING " + BaseColumns._ID + " = 1",
				from().having( BaseColumns._ID + " = 1").groupBy( BaseColumns._ID));
	}

	public void testAll() {
		final String expectedSql = SELECT_PREFIX + "AS a JOIN JoinModel USING (" +  BaseColumns._ID + ") WHERE " + BaseColumns._ID + " > 5 GROUP BY " + BaseColumns._ID + " HAVING " + BaseColumns._ID + " < 10 LIMIT 5 OFFSET 10";

		// Try a few different orderings, shouldn't change the output
		assertSqlEquals(expectedSql,
				from()
					.as("a")
					.where( BaseColumns._ID + " > 5")
					.join(JoinModel.class).using( BaseColumns._ID)
					.groupBy( BaseColumns._ID)
					.having( BaseColumns._ID + " < 10")
					.limit(5)
					.offset(10));
		assertSqlEquals(expectedSql,
				from()
					.offset(10)
					.having( BaseColumns._ID + " < 10")
					.join(JoinModel.class).using( BaseColumns._ID)
					.limit(5)
					.as("a")
					.where( BaseColumns._ID + " > 5")
					.groupBy( BaseColumns._ID));
		assertSqlEquals(expectedSql,
				from()
					.join(JoinModel.class).using( BaseColumns._ID)
					.offset(10)
					.having( BaseColumns._ID + " < 10")
					.where( BaseColumns._ID + " > 5")
					.groupBy( BaseColumns._ID)
					.limit(5)
					.as("a"));
	}

	private From from() {
		return new Select().all().from(MockModel.class);
	}

	@Table(name = "JoinModel")
	private static class JoinModel extends Model {
	}

	@Table(name = "JoinModel2")
	private static class JoinModel2 extends Model {
	}
}
