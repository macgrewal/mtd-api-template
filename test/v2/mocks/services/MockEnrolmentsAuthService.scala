/*
 * Copyright 2018 HM Revenue & Customs
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

package v2.mocks.services

import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v2.services.EnrolmentsAuthService
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import v2.models.ServiceResponse
import v2.models.errors.AuthError

import scala.concurrent.{ExecutionContext, Future}

trait MockEnrolmentsAuthService extends MockFactory {

  val mockEnrolmentsAuthService: EnrolmentsAuthService = mock[EnrolmentsAuthService]

  object MockedEnrolmentsAuthService {
    def authoriseUser(): Unit = {
      (mockEnrolmentsAuthService.authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(*, *, *)
        .returns(Future.successful(Right(true)))
    }

    def authorised(predicate: Predicate): CallHandler[ServiceResponse[AuthError, Boolean]] = {
      (mockEnrolmentsAuthService.authorised(_: Predicate)(_: HeaderCarrier, _: ExecutionContext))
        .expects(predicate, *, *)
    }
  }
}
