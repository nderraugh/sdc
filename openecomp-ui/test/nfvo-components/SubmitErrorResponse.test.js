/*
 * Copyright © 2016-2018 European Support Limited
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


import React from 'react';
import TestUtils from 'react-dom/test-utils';

import SubmitErrorResponse from 'nfvo-components/SubmitErrorResponse.jsx';
import {SubmitErrorMessageFactory} from 'test-utils/factories/SubnitErrorMessageFactorie.js';

describe('SubmitErrorResponse test: ', function () {
	it('basic render test', () => {
		let props = {
			validationResponse: SubmitErrorMessageFactory.build()
		};

		let view = TestUtils.renderIntoDocument(<SubmitErrorResponse {...props} />);
		expect(view).toBeTruthy();
	});
});
