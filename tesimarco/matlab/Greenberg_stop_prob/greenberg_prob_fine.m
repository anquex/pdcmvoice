% Marco Bettiol - 586580
% Batch Size Estimate

% Compute, for each slot i, the probability to finish basic greenberg
% algorithm in that slot given a batch of size n.
%
% Results are in table FINE(log2(n)+1,i)


clear all
close all
clc

%% probabilitià di trasmissione decrescente 2^-i

c=10000;            % numero cicli
N=17;                % (log del numero di nodi) +1
slots=N+6;           % maggiore numero nodi in quando possono trasmettere 'dopo'
x_i=[0:slots-1]'   % indici slot da 0 a N-1
n_i=2.^x_i % nodi attesi per slot (doppia trasposizione solo per avere il vettore in colonna)
format long
p_i=1./n_i         % probabilità di trasmettere     assegnata allo slot i




% i  indice riga    -> taglia 2^(i-1)
% ii indice colonna -> slot: 1,2,...
FINE=zeros(N,slots);

for i=1:N
    n=n_i(i); 
    for ii=2:slots
        p=1;
        %termina nello slot ii
        for t=2:ii-1
            p=p*( 1-( (1-p_i(t))^n+n*p_i(t)*(1-p_i(t))^(n-1) ) );
        end
        p=p*( (1-p_i(ii))^n+n*p_i(ii)*(1-p_i(ii))^(n-1) );
        FINE(i,ii-1)=p;
        %disp(['      ' int2str(ii) ' - ' num2str(p)]);
    end
    
end

% azzero elementi piccoli
for i=1:N
    for ii=1:slots
        if (FINE(i,ii)<10^-12)
            FINE(i,ii)=0;
        end
    end
end

% calcolo il valore atteso della stima dato n

E_n_stima=zeros(N,1);
for i=1:N
    n=n_i(i);
    stima_n=0;
    for k=1:slots
        stima_n=stima_n+n_i(k)*FINE(i,k);
    end
    E_n_stima(i)=stima_n;
end

E_n_stima;

ratio=E_n_stima./n_i(1:N);

% run greenberg_prob_fine_latex to display latex output


% plot for 1024,2048,4096
% n_i(11),n_i(12),n_i(13)       

close all;
hold on;
grid on;

set(gca, 'Box', 'off' ); % here gca means get current axis

h1=plot([1:slots],FINE(11,:),'k');
h2=plot([1:slots],FINE(12,:),'k');
h3=plot([1:slots],FINE(13,:),'k');
legend('n=1024','n=2048','n=4096');

hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 13);

hx=xlabel('slot','Interpreter','Latex');
hy=ylabel('stop probability','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);

fh = figure(1); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(h1, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');
set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);
set(h2, 'LineStyle', '-.', 'LineWidth', 1.0, 'Color',' Black');
set(h2, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);
set(h3, 'LineStyle', '--', 'LineWidth', 1.0, 'Color',' Black');
set(h3, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

axis([7 16 0 .5])
set(gca,'XTick',6:1:16);
saveas(fh, 'greenberg-stop-distribution-uniformity', 'epsc');





figure 
hold on;
grid on;

set(gca, 'Box', 'off' ); % here gca means get current axis

h1=plot([1:slots],FINE(4,:),'k');
h2=plot([1:slots],FINE(5,:),'k');
h3=plot([1:slots],FINE(6,:),'k');
legend('n=8','n=16','n=32');

hl = legend;
set(hl, 'Interpreter', 'Latex');
set(hl, 'FontSize', 13);

hx=xlabel('slot','Interpreter','Latex');
hy=ylabel('stop probability','Interpreter','Latex');
set(hx, 'FontSize', 15);
set(hy, 'FontSize', 15);

fh = figure(2); % returns the handle to the figure object
set(fh, 'color', 'white'); % sets the color to white

set(h1, 'LineStyle', '-', 'LineWidth', 1.0, 'Color', 'Black');
set(h1, 'Marker', 'o', 'MarkerFaceColor', [0.5 0.5 0.5], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);
set(h2, 'LineStyle', '-.', 'LineWidth', 1.0, 'Color',' Black');
set(h2, 'Marker', 's', 'MarkerFaceColor', [1 1 1], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);
set(h3, 'LineStyle', '--', 'LineWidth', 1.0, 'Color',' Black');
set(h3, 'Marker', 'd', 'MarkerFaceColor', [.8 .8 .8], 'MarkerEdgeColor', [0 0 0], 'MarkerSize', 5.0);

axis([0.99 9 0 .5])
set(gca,'XTick',0:1:10);
saveas(fh, 'greenberg-stop-distribution-uniformity-init', 'epsc');