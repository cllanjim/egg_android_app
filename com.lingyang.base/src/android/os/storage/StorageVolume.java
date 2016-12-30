/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.os.storage;

/**
 * A class representing a storage volume
 * 
 * @hide
 */
public class StorageVolume {

	/**
	 * Returns the mount path for the volume.
	 * 
	 * @return the mount path
	 */
	public String getPath() {
		return null;
	}

	/**
	 * Returns true if the volume is removable.
	 * 
	 * @return is removable
	 */
	public boolean isRemovable() {
		return false;
	}

	public boolean isEmulated() {
		return false;
	}

}
