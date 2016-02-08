/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.msf4j.internal.router;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matches incoming un-matched paths to destinations. Designed to be used for routing URI paths to http resources.
 * Parameters within braces "{}" are treated as template parameter (a named wild-card pattern).
 *
 * @param <T> represents the destination of the routes.
 */
public final class PatternPathRouterWithGroups<T> {

    //GROUP_PATTERN is used for named wild card pattern in paths which is specified within braces.
    //Example: {id}
    private static final Pattern GROUP_PATTERN = Pattern.compile("\\{(.*?)\\}");

    // non-greedy wild card match.
    private static final Pattern WILD_CARD_PATTERN = Pattern.compile("\\*\\*");

    private static final String PATH_SLASH = "/";

    private final List<ImmutablePair<Pattern, RouteDestinationWithGroups>> patternRouteList;

    /**
     * Initialize PatternPathRouterWithGroups.
     */
    public PatternPathRouterWithGroups() {
        this.patternRouteList = Lists.newArrayList();
    }

    public static <T> PatternPathRouterWithGroups<T> create() {
        return new PatternPathRouterWithGroups<>();
    }

    /**
     * Add a source and destination.
     *
     * @param source      Source path to be routed. Routed path can have named wild-card pattern with braces "{}".
     * @param destination Destination of the path.
     */
    public void add(final String source, final T destination) {

        // replace multiple slashes with a single slash.
        String path = source.replaceAll("/+", PATH_SLASH);

        path = (path.endsWith(PATH_SLASH) && path.length() > 1)
                ? path.substring(0, path.length() - 1) : path;


        String[] parts = path.split(PATH_SLASH);
        StringBuilder sb = new StringBuilder();
        List<String> groupNames = Lists.newArrayList();

        for (String part : parts) {
            Matcher groupMatcher = GROUP_PATTERN.matcher(part);
            if (groupMatcher.matches()) {
                groupNames.add(groupMatcher.group(1));
                sb.append("([^/]+?)");
            } else if (WILD_CARD_PATTERN.matcher(part).matches()) {
                sb.append(".*?");
            } else {
                sb.append(part);
            }
            sb.append(PATH_SLASH);
        }

        //Ignore the last "/"
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }

        Pattern pattern = Pattern.compile(sb.toString());
        patternRouteList.add(ImmutablePair.of(pattern, new RouteDestinationWithGroups(destination, groupNames)));
    }

    /**
     * Get a list of destinations and the values matching templated parameter for the given path.
     * Returns an empty list when there are no destinations that are matched.
     *
     * @param path path to be routed.
     * @return List of Destinations matching the given route.
     */
    public List<RoutableDestination<T>> getDestinations(String path) {

        String cleanPath = (path.endsWith(PATH_SLASH) && path.length() > 0)
                ? path.substring(0, path.length() - 1) : path;

        List<RoutableDestination<T>> result = Lists.newArrayList();

        for (ImmutablePair<Pattern, RouteDestinationWithGroups> patternRoute : patternRouteList) {
            ImmutableMap.Builder<String, String> groupNameValuesBuilder = ImmutableMap.builder();
            Matcher matcher = patternRoute.getFirst().matcher(cleanPath);
            if (matcher.matches()) {
                int matchIndex = 1;
                for (String name : patternRoute.getSecond().getGroupNames()) {
                    String value = matcher.group(matchIndex);
                    groupNameValuesBuilder.put(name, value);
                    matchIndex++;
                }
                result.add(new RoutableDestination<>(patternRoute.getSecond().getDestination(),
                        groupNameValuesBuilder.build()));
            }
        }
        return result;
    }

    /**
     * Represents a matched destination.
     *
     * @param <T> Type of destination.
     */
    public static final class RoutableDestination<T> {
        private final T destination;
        private final Map<String, String> groupNameValues;

        /**
         * Construct the RouteableDestination with the given parameters.
         *
         * @param destination     destination of the route.
         * @param groupNameValues parameters
         */
        public RoutableDestination(T destination, Map<String, String> groupNameValues) {
            this.destination = destination;
            this.groupNameValues = groupNameValues;
        }

        /**
         * @return destination of the route.
         */
        public T getDestination() {
            return destination;
        }

        /**
         * @return Map of templated parameter and string representation group value matching the templated parameter as
         * the value.
         */
        public Map<String, String> getGroupNameValues() {
            return groupNameValues;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("destination", destination)
                    .add("groupNameValues", groupNameValues)
                    .toString();
        }
    }

    /**
     * Helper class to store the groupNames and Destination.
     */
    private final class RouteDestinationWithGroups {

        private final T destination;
        private final List<String> groupNames;

        public RouteDestinationWithGroups(T destination, List<String> groupNames) {
            this.destination = destination;
            this.groupNames = groupNames;
        }

        public T getDestination() {
            return destination;
        }

        public List<String> getGroupNames() {
            return groupNames;
        }
    }
}
