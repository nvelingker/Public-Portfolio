/*
 * Copyright 2016 Jon Ander Peñalba
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

import java.util.Date;

public class Milestone {
    public long id;

    public int number;

    public String state;

    public String title;

    public String description;

    public User creator;

    public int open_issues;

    public int closed_issues;

    public Date created_at;

    public Date updated_at;

    public Date closed_at;

    public Date due_on;
}
