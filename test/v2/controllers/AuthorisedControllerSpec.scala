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

package v2.controllers

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.Enrolment
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import v2.models.errors.{AuthError, InvalidNino}
import v2.outcomes.MtdIdLookupOutcome.{DownstreamError, NotAuthorised}
import v2.services.{EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.Future

class AuthorisedControllerSpec extends ControllerBaseSpec {

  trait Test extends MockEnrolmentsAuthService with MockMtdIdLookupService {
    val hc = HeaderCarrier()

    class TestController extends AuthorisedController {
      override val authService: EnrolmentsAuthService = mockEnrolmentsAuthService
      override val lookupService: MtdIdLookupService = mockMtdIdLookupService

      def action(nino: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>
        Future.successful(Ok(Json.obj()))
      }
    }

    lazy val target = new TestController()
  }

  val nino = "AA123456A"
  val mtdId = "X123567890"
  val predicate: Predicate = Enrolment("HMRC-MTD-IT")
    .withIdentifier("MTDITID", mtdId)
    .withDelegatedAuthRule("mtd-it-auth")

  "calling an action" when {

    "the user is authorised" should {
      "return a 200" in new Test {

        MockedMtdIdLookupService.lookup(nino)
          .returns(Future.successful(Right(mtdId)))

        MockedEnrolmentsAuthService.authoriseUser()

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe OK
      }
    }

    "the nino is invalid" should {
      "return a 400" in new Test {

        MockedMtdIdLookupService.lookup(nino)
          .returns(Future.successful(Left(InvalidNino)))

        private val result = target.action(nino)(fakeGetRequest)
        status(result) shouldBe BAD_REQUEST
      }
    }

  }

  "authorisation checks fail when retrieving the MDT ID" should {
    "return a 403" in new Test {

      MockedMtdIdLookupService.lookup(nino)
        .returns(Future.successful(Left(NotAuthorised)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe FORBIDDEN
    }
  }

  "the an error occurs retrieving the MDT ID" should {
    "return a 500" in new Test {

      MockedMtdIdLookupService.lookup(nino)
        .returns(Future.successful(Left(DownstreamError)))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "the MTD user is not authenticated" should {
    "return a 200" in new Test {

      MockedMtdIdLookupService.lookup(nino)
        .returns(Future.successful(Right(mtdId)))

      MockedEnrolmentsAuthService.authorised(predicate)
        .returns(Future.successful(Left(AuthError())))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe 401
    }
  }

  "the MTD user is not authorised" should {
    "return a 200" in new Test {

      MockedMtdIdLookupService.lookup(nino)
        .returns(Future.successful(Right(mtdId)))

      MockedEnrolmentsAuthService.authorised(predicate)
        .returns(Future.successful(Left(AuthError(true, false))))

      private val result = target.action(nino)(fakeGetRequest)
      status(result) shouldBe 403
    }
  }

}
