/*
 * Copyright 2011,2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.parsers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.metamx.common.collect.Utils;
import com.metamx.common.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DelimitedParser implements Parser<String, Object>
{
  private static final Logger log = new Logger(DelimitedParser.class);
  private static final String DEFAULT_DELIMITER = "\t";

  private final String delimiter;
  private final String listDelimiter;

  private final Splitter splitter;
  private final Splitter listSplitter;
  private final Function<String, Object> valueFunction;

  private ArrayList<String> fieldNames = null;

  public DelimitedParser(final Optional<String> delimiter, Optional<String> listDelimiter)
  {
    this.delimiter = delimiter.isPresent() ? delimiter.get() : DEFAULT_DELIMITER;
    this.listDelimiter = listDelimiter.isPresent() ? listDelimiter.get() : Parsers.DEFAULT_LIST_DELIMITER;
    this.splitter = Splitter.on(this.delimiter);
    this.listSplitter = Splitter.on(this.listDelimiter);
    this.valueFunction = new Function<String, Object>()
    {
      @Override
      public Object apply(String input)
      {
        if (input.contains(DelimitedParser.this.listDelimiter)) {
          return Lists.newArrayList(
              Iterables.transform(
                  listSplitter.split(input),
                  ParserUtils.nullEmptyStringFunction
              )
          );
        } else {
          return ParserUtils.nullEmptyStringFunction.apply(input);
        }
      }
    };
  }

  public DelimitedParser(
      final Optional<String> delimiter,
      final Optional<String> listDelimiter,
      final Iterable<String> fieldNames
  )
  {
    this(delimiter, listDelimiter);

    setFieldNames(fieldNames);
  }

  public DelimitedParser(final Optional<String> delimiter, final Optional<String> listDelimiter, final String header)
  {
    this(delimiter, listDelimiter);

    setFieldNames(header);
  }

  public String getDelimiter()
  {
    return delimiter;
  }

  public String getListDelimiter()
  {
    return listDelimiter;
  }

  @Override
  public List<String> getFieldNames()
  {
    return fieldNames;
  }

  @Override
  public void setFieldNames(final Iterable<String> fieldNames)
  {
    ParserUtils.validateFields(fieldNames);
    this.fieldNames = Lists.newArrayList(fieldNames);
  }

  public void setFieldNames(String header)
  {
    try {
      setFieldNames(splitter.split(header));
    }
    catch (Exception e) {
      log.error(e, "Unable to parse header [%s]", header);
      throw Throwables.propagate(e);
    }
  }

  @Override
  public Map<String, Object> parse(final String input)
  {
    try {
      Iterable<String> values = splitter.split(input);

      if (fieldNames == null) {
        setFieldNames(ParserUtils.generateFieldNames(Iterators.size(values.iterator())));
      }

      return Utils.zipMapPartial(fieldNames, Iterables.transform(values, valueFunction));
    }
    catch (Exception e) {
      log.error(e, "Unable to parse row [%s]", input);
      throw Throwables.propagate(e);
    }
  }
}
