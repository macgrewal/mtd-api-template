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

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import v2.services.{EnrolmentsAuthService, MtdIdLookupService}

import scala.concurrent.Future

@Singleton
class SampleController @Inject()(val authService: EnrolmentsAuthService,
                                 val lookupService: MtdIdLookupService) extends AuthorisedController {

  def doSomething(nino: String): Action[AnyContent] = authorisedAction(nino).async { implicit request =>
    Future.successful(Ok(request.mtdId))
  }
}
