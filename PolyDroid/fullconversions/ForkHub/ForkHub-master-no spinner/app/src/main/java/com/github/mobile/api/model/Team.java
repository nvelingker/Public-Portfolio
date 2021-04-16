/*
 * Copyright 2017 Jon Ander Peñalba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile.api.model;

import java.io.Serializable;

public class Team implements Serializable {
    public long id;

    public String url;

    public String name;

    public String slug;

    public String description;

    public String privacy;

    public String permission;

    public String members_url;

    public String repositories_url;

    public int members_count;

    public int repos_count;

    public User organization;
}
