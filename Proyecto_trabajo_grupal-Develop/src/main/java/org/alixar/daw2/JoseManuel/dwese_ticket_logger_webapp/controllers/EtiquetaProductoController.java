package org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.controllers;

import jakarta.validation.Valid;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.Caja;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.entities.EtiquetaProducto;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.CajaRepository;
import org.alixar.daw2.JoseManuel.dwese_ticket_logger_webapp.repositories.EtiquetaProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Controlador encargado de gestionar las operaciones relacionadas
 * con las etiquetas de producto del almacén.
 * Permite listar, crear, editar y eliminar etiquetas, además de
 * gestionar las validaciones y los posibles errores.
 */
@Controller
@RequestMapping("/etiquetas")
public class EtiquetaProductoController {

    private static final Logger logger =
            LoggerFactory.getLogger(EtiquetaProductoController.class);

    @Autowired
    private EtiquetaProductoRepository etiquetaProductoRepository;

    @Autowired
    private CajaRepository cajaRepository;

    @Autowired
    private MessageSource messageSource;

    /**
     * Muestra la lista de todas las etiquetas registradas.
     * Obtiene los datos de la base de datos y los envía a la vista.
     * Si se produce un error, muestra un mensaje de error.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista que muestra la lista de etiquetas
     */
    @GetMapping
    public String listEtiquetas(Model model, Locale locale) {
        logger.info("Solicitando la lista de etiquetas...");

        List<EtiquetaProducto> listEtiquetas = null;

        try {
            listEtiquetas = etiquetaProductoRepository.findAll();
            logger.info("Se han cargado {} etiquetas.", listEtiquetas.size());
        } catch (DataAccessException e) {
            logger.error("Error al listar las etiquetas: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.list.error", null, locale));
        }

        model.addAttribute("listEtiquetas", listEtiquetas);

        return "etiquetas/etiqueta";
    }

    /**
     * Muestra el formulario para crear una nueva etiqueta.
     * Inicializa un objeto EtiquetaProducto y una caja vacía para
     * poder seleccionar una en el formulario.
     *
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario de etiquetas
     */
    @GetMapping("/new")
    public String showNewForm(Model model, RedirectAttributes redirectAttributes,
                              Locale locale) {
        logger.info("Mostrando formulario para nueva etiqueta.");

        EtiquetaProducto etiqueta = new EtiquetaProducto();
        // Caja vacía para que el formulario pueda enlazar caja.id.
        etiqueta.setCaja(new Caja());

        model.addAttribute("etiqueta", etiqueta);

        return loadCajasAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Muestra el formulario para editar una etiqueta existente.
     * Busca la etiqueta mediante su identificador y comprueba
     * que exista antes de mostrar el formulario.
     *
     * @param id identificador de la etiqueta que se quiere editar
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de etiquetas si se produce algún error
     */
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id, Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para la etiqueta con ID {}", id);

        try {
            Optional<EtiquetaProducto> etiqueta = etiquetaProductoRepository.findById(id);

            if (etiqueta.isEmpty()) {
                logger.warn("No se encontró la etiqueta con ID {}", id);

                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.etiqueta-controller.edit.notFound", null, locale));

                return "redirect:/etiquetas";
            }

            model.addAttribute("etiqueta", etiqueta.get());

        } catch (DataAccessException e) {
            logger.error("Error al obtener la etiqueta con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.edit.error", null, locale));

            return "redirect:/etiquetas";
        }

        return loadCajasAndShowForm(model, redirectAttributes, locale);
    }

    /**
     * Recibe los datos del formulario para insertar una nueva etiqueta.
     * Comprueba que los datos sean válidos, que el código de barras no
     * esté repetido y que la caja elegida no tenga ya otra etiqueta
     * antes de guardar la etiqueta en la base de datos.
     *
     * @param etiqueta objeto con los datos de la etiqueta recibidos del formulario
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de etiquetas
     */
    @PostMapping("/insert")
    public String insertEtiqueta(@Valid @ModelAttribute("etiqueta") EtiquetaProducto etiqueta,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Insertando nueva etiqueta con código {}", etiqueta.getCodigoBarras());

        try {
            checkCajaSelected(etiqueta, result);

            // Solo se consulta la base de datos si el campo no tiene ya errores.
            if (!result.hasFieldErrors("codigoBarras")
                    && etiquetaProductoRepository.existsByCodigoBarras(etiqueta.getCodigoBarras())) {
                logger.warn("El código de barras {} ya existe.", etiqueta.getCodigoBarras());

                result.rejectValue("codigoBarras",
                        "msg.etiqueta-controller.insert.codeExist");
            }

            if (!result.hasFieldErrors("caja.id")
                    && etiquetaProductoRepository.existsByCajaId(etiqueta.getCaja().getId())) {
                logger.warn("La caja {} ya tiene una etiqueta asignada.",
                        etiqueta.getCaja().getId());

                result.rejectValue("caja.id",
                        "msg.etiqueta-controller.insert.cajaTaken");
            }

            if (result.hasErrors()) {
                return loadCajasAndShowForm(model, redirectAttributes, locale);
            }

            etiquetaProductoRepository.save(etiqueta);

            logger.info("Etiqueta insertada con éxito.");

        } catch (DataAccessException e) {
            logger.error("Error al insertar la etiqueta: {}", e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.insert.error", null, locale));
            try {
                model.addAttribute("listCajas", cajaRepository.findAll());
                return "etiquetas/etiqueta-form";
            } catch (DataAccessException loadException) {
                logger.error("Error al recargar las cajas: {}", loadException.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.etiqueta-controller.cajas.error", null, locale));
                return "redirect:/etiquetas";
            }
        }

        return "redirect:/etiquetas";
    }

    /**
     * Recibe los datos del formulario para actualizar una etiqueta existente.
     * Comprueba que los datos sean válidos, que el código de barras no
     * pertenezca a otra etiqueta y que la caja elegida no tenga ya otra
     * etiqueta antes de guardar los cambios en la base de datos.
     *
     * @param etiqueta objeto con los datos actualizados de la etiqueta
     * @param result resultado de las validaciones del formulario
     * @param model modelo utilizado para enviar los datos a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario si hay errores o redirección
     *         a la lista de etiquetas
     */
    @PostMapping("/update")
    public String updateEtiqueta(@Valid @ModelAttribute("etiqueta") EtiquetaProducto etiqueta,
                                 BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Actualizando etiqueta con ID {}", etiqueta.getId());

        try {
            checkCajaSelected(etiqueta, result);

            if (!result.hasFieldErrors("codigoBarras")
                    && etiquetaProductoRepository.existsByCodigoBarrasAndIdNot(
                    etiqueta.getCodigoBarras(), etiqueta.getId())) {
                logger.warn("El código de barras {} ya existe para otra etiqueta.",
                        etiqueta.getCodigoBarras());

                result.rejectValue("codigoBarras",
                        "msg.etiqueta-controller.update.codeExist");
            }

            if (!result.hasFieldErrors("caja.id")
                    && etiquetaProductoRepository.existsByCajaIdAndIdNot(
                    etiqueta.getCaja().getId(), etiqueta.getId())) {
                logger.warn("La caja {} ya tiene otra etiqueta asignada.",
                        etiqueta.getCaja().getId());

                result.rejectValue("caja.id",
                        "msg.etiqueta-controller.update.cajaTaken");
            }

            if (result.hasErrors()) {
                return loadCajasAndShowForm(model, redirectAttributes, locale);
            }

            etiquetaProductoRepository.save(etiqueta);

            logger.info("Etiqueta con ID {} actualizada con éxito.", etiqueta.getId());

        } catch (DataAccessException e) {
            logger.error("Error al actualizar la etiqueta con ID {}: {}",
                    etiqueta.getId(), e.getMessage());

            model.addAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.update.error", null, locale));
            try {
                model.addAttribute("listCajas", cajaRepository.findAll());
                return "etiquetas/etiqueta-form";
            } catch (DataAccessException loadException) {
                logger.error("Error al recargar las cajas: {}", loadException.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage(
                                "msg.etiqueta-controller.cajas.error", null, locale));
                return "redirect:/etiquetas";
            }
        }

        return "redirect:/etiquetas";
    }

    /**
     * Elimina una etiqueta mediante su identificador.
     *
     * @param id identificador de la etiqueta que se quiere eliminar
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return redirección a la lista de etiquetas
     */
    @PostMapping("/delete")
    public String deleteEtiqueta(@RequestParam("id") Long id,
                                 RedirectAttributes redirectAttributes,
                                 Locale locale) {
        logger.info("Eliminando etiqueta con ID {}", id);

        try {
            etiquetaProductoRepository.deleteById(id);

            logger.info("Etiqueta con ID {} eliminada con éxito.", id);

        } catch (DataAccessException e) {
            logger.error("Error al eliminar la etiqueta con ID {}: {}", id, e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.delete.error", null, locale));
        }

        return "redirect:/etiquetas";
    }

    /**
     * Comprueba que se haya seleccionado una caja en el desplegable.
     * Si no se ha seleccionado ninguna, añade un error de validación
     * al campo correspondiente.
     *
     * @param etiqueta etiqueta cuyos datos se quieren comprobar
     * @param result resultado donde se registra el error de validación
     */
    private void checkCajaSelected(EtiquetaProducto etiqueta, BindingResult result) {
        if (etiqueta.getCaja() == null || etiqueta.getCaja().getId() == null) {
            result.rejectValue("caja.id", "msg.etiqueta.caja.notNull");
        }
    }

    /**
     * Carga la lista de cajas y la añade al modelo para mostrar
     * el formulario de creación o edición de una etiqueta.
     * Si se produce un error al obtener las cajas, realiza
     * una redirección a la lista de etiquetas mostrando un mensaje.
     *
     * @param model modelo utilizado para enviar las cajas a la vista
     * @param redirectAttributes atributos utilizados para enviar mensajes
     *                           durante las redirecciones
     * @param locale configuración regional para obtener los mensajes
     * @return nombre de la vista del formulario o redirección a la lista
     *         de etiquetas si se produce un error
     */
    private String loadCajasAndShowForm(Model model,
                                        RedirectAttributes redirectAttributes,
                                        Locale locale) {
        try {
            model.addAttribute("listCajas", cajaRepository.findAll());

            return "etiquetas/etiqueta-form";

        } catch (DataAccessException e) {
            logger.error("Error al cargar las cajas: {}", e.getMessage());

            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage(
                            "msg.etiqueta-controller.cajas.error", null, locale));

            return "redirect:/etiquetas";
        }
    }
}
