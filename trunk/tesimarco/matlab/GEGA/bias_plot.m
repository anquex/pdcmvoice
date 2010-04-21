% bias plot

close all;
clear all;
hold all;
clc;

load('GMLE-bias-T10');
h10=plot(batchsizes,bias);
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

load(['GMLE-bias-T20']);
h20=plot(batchsizes,bias);
clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

load(['GMLE-bias-T30']);
h30=plot(batchsizes,bias);
%clear fiscpn i k l n p p_idle p_succ_cond_idle prb_stop_slot D GMLE

legend('$T=10$','$T=20$','$T=30$','Location','Best');
hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 11);

hx=xlabel('$n$','Interpreter','Latex');
hy=ylabel('$E[\hat{n}]/n$','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);



fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(gca, 'Box', 'on' ); % here gca means get current axis
grid on;

set(h10, 'LineStyle', '-.', 'LineWidth', 1.1, 'Color', 'Black');
set(h20, 'LineStyle', '--', 'LineWidth', 1.1, 'Color', 'Black');
set(h30, 'LineStyle', '-', 'LineWidth', 1.1, 'Color', 'Black');

%set(gca,'YTickLabel',)
set(gca,'YLim',[1.029 1.075])
set(gca,'XLim',[5 2000])
set(gca,'XScale','log');