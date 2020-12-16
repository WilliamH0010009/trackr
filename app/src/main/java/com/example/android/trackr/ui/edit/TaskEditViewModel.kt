/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.trackr.ui.edit

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.example.android.trackr.data.User
import com.example.android.trackr.db.dao.TaskDao
import kotlinx.coroutines.launch

class TaskEditViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    var taskId: Long = 0L
        set(value) {
            field = value
            loadInitialData(value)
        }

    val title = MutableLiveData("")

    val description = MutableLiveData("")

    private val _owner = MutableLiveData<User>()
    val owner: LiveData<User> = _owner

    private val _creator = MutableLiveData<User>()
    val creator: LiveData<User> = _creator

    lateinit var users: List<User>
        private set

    private val _modified = MediatorLiveData<Boolean>().apply {
        val sources = listOf(title, description)
        var sourceCount = sources.size
        for (source in sources) {
            addSource(source.distinctUntilChanged()) {
                // Ignore initial data from each of the sources.
                if (sourceCount <= 0) {
                    value = true
                } else {
                    --sourceCount
                }
            }
        }
        value = false
    }

    /**
     * Whether any of the content is modified or not.
     */
    val modified: LiveData<Boolean> = _modified

    private fun loadInitialData(taskId: Long) {
        viewModelScope.launch {
            users = taskDao.loadUsers()
            if (taskId != 0L) {
                val detail = taskDao.loadTaskDetailById(taskId)
                if (detail != null) {
                    title.value = detail.title
                    description.value = detail.description
                    _owner.value = detail.owner
                    _creator.value = detail.reporter
                    _modified.value = false
                }
            }
        }
    }

    fun updateOwner(user: User) {
        _owner.value = user
        _modified.value = true
    }

    fun save() {
        TODO("Save is not yet implemented")
    }
}